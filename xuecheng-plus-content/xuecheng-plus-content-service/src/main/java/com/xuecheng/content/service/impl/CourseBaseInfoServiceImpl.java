package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数合法性校验，注意有可能不被controller调用，那就需要校验
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XueChengPlusException.cast("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

        // 向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        // 将传入的页面的参数放到对象中
//        courseBase.setName(dto.getName());
//        courseBase.setDescription(dto.getDescription());
//        这种方式比较复杂
        BeanUtils.copyProperties(dto, courseBaseNew);   // 属性名一致即可拷贝
        // 如果原来是null，拷贝后null会覆盖存在的值
        courseBaseNew.setCompanyId(companyId);          // 这里放后边是怕null覆盖
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 审核状态默认为未提交
        courseBaseNew.setAuditStatus("202002");
        // 发布状态为未发布
        courseBaseNew.setStatus("203001");
        // 插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            throw new RuntimeException("添加课程失败");
        }
        // 向课程营销course_market表写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        //  主键的课程id，这里用了mybatis-plus默认方法的主键回显
        Long courseId = courseBaseNew.getId();
        // 将页面输入的数据拷贝到marketNew
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseId);
        // 保存营销信息
        saveCourseMarket(courseMarketNew);
        // 从数据库中查询课程的详细信息，包括两部分：基本信息+营销信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    // 查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        // 从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            return null;
        }
        // 从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        // 这里BeanUtils可能会报空指针异常
        if(courseBase!=null){
            BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        }
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // TODO:课程分类的名称设置到对象中
        CourseCategory mt = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        CourseCategory st = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        if(mt!=null){
            courseBaseInfoDto.setMtName(mt.getName());
        }
        if(st!=null){
            courseBaseInfoDto.setStName(st.getName());
        }

        return courseBaseInfoDto;
    }


    // 单独写一个方法保存营销信息，逻辑：存在则更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        // 参数合法性校验
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isEmpty(charge)){
            throw new RuntimeException("收费规则为空");
        }
        if(charge.equals("201001")){
            // 必须是一个收费课程才能有效
            if(courseMarketNew.getPrice()==null || courseMarketNew.getPrice()<=0){
                XueChengPlusException.cast("价格不能为空且必须大于0");
            }
        }
        // 从数据库查询营销信息，存在则更新，不存在则新增
        Long id = courseMarketNew.getId();
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if(courseMarket == null){
            // 插入数据库
            int insert = courseMarketMapper.insert(courseMarketNew);
            return insert;
        } else{
            // 更新数据库
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            // 更新
            int i = courseMarketMapper.updateById(courseMarket);
            return i;
        }
    }
}
