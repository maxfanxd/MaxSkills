package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
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

    /**
     *  根据课程id查询课程信息
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);
}
