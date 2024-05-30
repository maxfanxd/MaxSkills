package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

// 课程计划查询的接口
public interface TeachplanService {
    /**
     * 根据id查询课程计划
     * @param courseId
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新赠/修改/保存课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
