package com.airs.controller;

import com.airs.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menu")
public class MenuController {

    @GetMapping
    public Result<List<Map<String, Object>>> getMenus() {
        List<Map<String, Object>> menus = Arrays.asList(
                createMenu("dashboard", "仪表盘", "DataLine", "/dashboard"),
                createMenu("athletes", "运动员管理", "User", "/athletes"),
                createMenu("injury", "伤病记录", "Document", "/injury"),
                createMenu("rehabilitation", "康复计划", "Medic", "/rehabilitation"),
                createMenu("training", "训练计划", "Trophy", "/training"),
                createMenu("users", "用户管理", "Setting", "/users")
        );
        return Result.success(menus);
    }

    private Map<String, Object> createMenu(String name, String title, String icon, String path) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("name", name);
        menu.put("title", title);
        menu.put("icon", icon);
        menu.put("path", path);
        return menu;
    }
}
