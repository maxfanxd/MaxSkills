package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

public interface CourseBaseInfoService {
    /**
     * 课程分页查询
     * @param pageParams
     * @param courseParamsDto
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 新增课程信息
     * @param addCourseDto
     * @param companyId
     * @return
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);
}
