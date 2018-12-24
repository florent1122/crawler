package com.gm.demo.crawler.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jason
 */
@Data
@ApiModel("美团数据请求实体")
public class MtDataReq implements Serializable {
    @ApiModelProperty("数据表")
    private String tab;
    @ApiModelProperty("是否爬取")
    private Integer isCrawl;
}