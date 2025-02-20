package com.hella.ictmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shopFloorMap")
public class SiteMapController {

    @GetMapping
    public String showShopFloor() {
        return "siteMap/shopFloorMap";
    }
}