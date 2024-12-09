package com.example.onlineshop.controller;

import com.example.onlineshop.model.dto.GoodDTO;
import com.example.onlineshop.service.GoodsService;
import com.example.onlineshop.service.OrdersService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletionException;

@AllArgsConstructor
@Controller
public class PageController {
    GoodsService goodsService;
    OrdersService ordersService;

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .noneMatch(auth -> auth.toString().equals("ROLE_ANONYMOUS"))) {
            return "redirect:/";
        }
        return "signin";
    }

    @GetMapping("/register")
    public String register() {
        return "signup";
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "name", required = false) String name, Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        if (name != null){
            try{
                List<GoodDTO> goods = goodsService.findGoods(name).join();
                model.addAttribute("goods", goods);
                model.addAttribute("numfound", goods.size());
            } catch (CompletionException ig){
                model.addAttribute("numfound", 0);
                model.addAttribute("error", true);
            }
        } else {
            try{
                List<GoodDTO> goods = goodsService.getGoodsList().join();
                model.addAttribute("goods", goods);
                model.addAttribute("numfound", goods.size());
            } catch (CompletionException ig){
                model.addAttribute("numfound", 0);
                model.addAttribute("error", true);
            }
        }
        return "index";
    }

    @GetMapping("/good/{id}")
    public String goods(@PathVariable Integer id, Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        return "good";
    }

    @GetMapping("/orderslist")
    public String orders(Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        return "orders";
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/mycart")
    public String cart(Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        return "cart";
    }
}
