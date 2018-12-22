package com.gm.demo.crawler.controller;

import com.gm.demo.crawler.entity.req.CrawlReq;
import com.gm.demo.crawler.service.MtCrawlerServiceImpl;
import com.gm.help.base.Quick;
import com.gm.model.response.HttpResult;
import com.gm.model.response.JsonResult;
import com.gm.strong.Rules;
import com.gm.utils.base.ExceptionUtils;
import com.gm.utils.base.Logger;
import com.gm.utils.ext.Web;
import com.gm.utils.third.Http;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author Jason
 */
@RestController
@Api(tags = "美团爬虫控制器")
@RequestMapping("mt/crawler")
public class MtCrawlerController {

    public static final String offset = "[offset]";
    public static final String pageSize = "[pageSize]";
    public static final String pageNo = "[pageNo]";
    public static final String id = "[id]";

    @Autowired
    MtCrawlerServiceImpl mtCrawlerService;

    @PostMapping("merchant")
    @ApiOperation(value = "释放一只商家爬虫")
    public JsonResult merchant(@RequestBody @Valid CrawlReq req) {
        Map<String, String> params = Web.getParams(req.getUrl());
        if (params.containsValue(pageNo)) {
            Integer total = merchantPages(req.getUrl(), req.getHeaders(), req.getParams());
            return JsonResult.as(total);
        }
        return JsonResult.unsuccessful("请设置翻页规则[field]..");
    }

    @PostMapping("comment")
    @ApiOperation(value = "释放一只评论爬虫")
    public JsonResult comment(@RequestBody @Valid CrawlReq req) {
        Map<String, String> params = Web.getParams(req.getUrl());
        if (params.containsValue(offset) && params.containsValue(pageSize)) {
            Integer total = commentPages(req.getUrl(), req.getHeaders(), req.getParams());
            return JsonResult.as(total);
        }
        return JsonResult.unsuccessful("请设置翻页规则[field]..");
    }

    /**
     * 分页爬取商家数据
     *
     * @param url
     * @param headers
     * @param params
     * @return
     */
    private Integer merchantPages(final String url, Map<String, String> headers, Map<String, Object> params) {
        Integer[] sum = {0};
        P page = new P(1);
        Quick.echo(x -> {
            String newUrl = Rules.parse(page, url);
            HttpResult result = Http.doGet(newUrl, headers, params);
            if (!JsonResult.SUCCESS.equals(result.getStatus())) {
                result = Http.doPost(newUrl, headers, params);
                if (!JsonResult.SUCCESS.equals(result.getStatus())) {
                    JsonResult.unsuccessful(new String(result.getResult()));
                }
            }
            String json = new String(result.getResult());
            sum[0] += mtCrawlerService.handlerMerchant(json);
            Logger.debug("gather:   ".concat(sum[0].toString()).concat("\n").concat(newUrl));
            // 从这里开始
            page.setPageNo(page.pageNo + 1);
        });
        return sum[0];
    }

    /**
     * 分页爬取评论数据
     *
     * @param url
     * @param headers
     * @param params
     * @return
     */
    private Integer commentPages(final String url, Map<String, String> headers, Map<String, Object> params) {
        Integer[] sum = {0};
        P page = new P(1, 100);
        Quick.echo(x -> {
            String newUrl = Rules.parse(page, url);
            HttpResult result = Http.doGet(newUrl, headers, params);
            if (!JsonResult.SUCCESS.equals(result.getStatus())) {
                result = Http.doPost(newUrl, headers, params);
                if (!JsonResult.SUCCESS.equals(result.getStatus())) {
                    JsonResult.unsuccessful(new String(result.getResult()));
                }
            }
            String json = new String(result.getResult());
            sum[0] += mtCrawlerService.handlerComment(json);
            Logger.debug("gather:   ".concat(sum[0].toString()).concat("\n").concat(newUrl));
            // 从这里开始
            page.setOffset(page.offset + page.pageSize);
        });
        return sum[0];
    }

    /**
     * 分页对象
     */
    @Data
    static class P {
        private Integer offset;
        private Integer pageNo;
        private Integer pageSize;

        public P(Integer pageNo) {
            this.pageNo = pageNo;
        }

        public P(Integer offset, Integer pageSize) {
            this.offset = offset;
            this.pageSize = pageSize;
        }
    }
}
