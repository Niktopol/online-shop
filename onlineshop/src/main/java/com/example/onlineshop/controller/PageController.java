package com.example.onlineshop.controller;

import com.example.onlineshop.model.dto.*;
import com.example.onlineshop.service.AuthService;
import com.example.onlineshop.service.GoodsService;
import com.example.onlineshop.service.OrdersService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletionException;

@AllArgsConstructor
@Controller
public class PageController {
    GoodsService goodsService;
    OrdersService ordersService;
    AuthService authService;

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

    @PostMapping("/register")
    public String createAccount(UserDTO userData, Model model) {
        if (userData.getName().isEmpty() || userData.getUsername().isEmpty() || userData.getPassword().isEmpty()){
            model.addAttribute("error", true);
            model.addAttribute("msg", "Пожалуйста, заполните все поля");
            return "signup";
        }
        if (authService.signUp(userData).isEmpty()){
            return "redirect:/login?success=true";
        } else {
            model.addAttribute("error", true);
            model.addAttribute("msg", "Указанный логин занят");
            return "signup";
        }
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String name, Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        if (name != null){
            try{
                List<GoodDTO> goods = goodsService.findGoods(name).join();
                model.addAttribute("goods", goods);
                model.addAttribute("numfound", goods.size());
            } catch (Exception e){
                model.addAttribute("numfound", 0);
                model.addAttribute("error", true);
            }
        } else {
            try{
                List<GoodDTO> goods = goodsService.getGoodsList().join();
                model.addAttribute("goods", goods);
                model.addAttribute("numfound", goods.size());
            } catch (Exception e){
                model.addAttribute("numfound", 0);
                model.addAttribute("error", true);
            }
        }
        return "index";
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping("/")
    public String addGood(@RequestParam String name, @RequestParam String price) {
        if (name.isEmpty() || price.isEmpty()){
            return "redirect:/?errempt=true";
        }else{
            double priceNum;
            try{
                priceNum = Math.round(Double.parseDouble(price) * 100.0) / 100.0;
            } catch (NumberFormatException e){
                return "redirect:/?errnum=true";
            }
            try{
                goodsService.addGoods(List.of(new GoodAddDTO(name, priceNum))).join();
            } catch (CompletionException e){
                if (e.getCause() instanceof StatusRuntimeException){
                    if (((StatusRuntimeException) e.getCause()).getStatus().getCode() == Status.Code.ALREADY_EXISTS){
                        return "redirect:/?errtkn=true";
                    } else {
                        return "redirect:/?adderr=true";
                    }
                } else {
                    return "redirect:/?adderr=true";
                }
            } catch (Exception e){
                return "redirect:/?adderr=true";
            }
            return "redirect:/?addsucc=true";
        }
    }

    @GetMapping("/good/{id}")
    public String goods(@PathVariable Long id, Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        try {
            GoodDTO good = goodsService.getGood(id).join();
            model.addAttribute("good", good);
        } catch (Exception e){
            model.addAttribute("error", true);
        }
        return "good";
    }

    @PostMapping("/good/{id}")
    public String goodsUpdate(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String price,
                              @RequestParam String amount,
                              @RequestParam String canBeSold){
        AlterGoodDTO data = new AlterGoodDTO(id, null, null, null, null);

        if (!name.isEmpty()){
            data.setName(name);
        }

        if (!price.isEmpty()){
            try {
                data.setPrice(Math.round(Double.parseDouble(price) * 100.0) / 100.0);
            } catch (NumberFormatException e) {
                return "redirect:/good/" + id + "?errnum=true";
            }
        }

        if (!amount.isEmpty()){
            try {
                data.setAmount(Integer.parseInt(amount));
            } catch (NumberFormatException e) {
                return "redirect:/good/" + id + "?errnum=true";
            }
        }

        if (!canBeSold.isEmpty()){
            try {
                data.setCanBeSold(Integer.parseInt(canBeSold) != 0);
            } catch (NumberFormatException e) {
                return "redirect:/good/" + id + "?alterr=true";
            }
        }

        try {
            goodsService.alterGoods(List.of(data)).join();
        } catch (CompletionException e){
            if (e.getCause() instanceof StatusRuntimeException){
                if (((StatusRuntimeException) e.getCause()).getStatus().getCode() == Status.Code.ALREADY_EXISTS){
                    return "redirect:/good/" + id + "?errtkn=true";
                } else {
                    return "redirect:/good/" + id + "?alterr=true";
                }
            } else {
                return "redirect:/good/" + id + "?alterr=true";
            }
        } catch (Exception e){
            return "redirect:/good/" + id + "?alterr=true";
        }

        return "redirect:/good/" + id + "?addsucc=true";
    }

    @GetMapping("/orderslist")
    public String orders(@RequestParam(required = false) String minStat,
                         @RequestParam(required = false) String maxStat,
                         Model model) {
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString();
        model.addAttribute("role", role);
        if (role.equals("CUSTOMER")){
            try {
                List<OrderDTO> orders = ordersService.getOrders().join();
                model.addAttribute("orders", orders);
                model.addAttribute("numfound", orders.size());
            } catch (Exception e) {
                model.addAttribute("error", true);
                model.addAttribute("numfound", 0);
                return "orders";
            }
        } else if (role.equals("OWNER")){
            if (minStat != null && maxStat == null || minStat == null && maxStat != null){
                return "redirect:/orderslist";
            } else if (minStat != null) {
                int min;
                int max;
                try {
                    min = Integer.parseInt(minStat);
                    max = Integer.parseInt(maxStat);
                    if (min > max){
                        return "redirect:/orderslist?minStat=" + max + "&maxStat=" + max;
                    } else if(min < 0 || min > 3 || max > 3){
                        return "redirect:/orderslist";
                    } else {
                        model.addAttribute("minStat", min);
                        model.addAttribute("maxStat", max);
                        try {
                            List<OrderAndUserDTO> orders = ordersService.getOrdersByStatus(min, max).join();
                            model.addAttribute("orders", orders);
                            model.addAttribute("numfound", orders.size());
                            return "orders";
                        } catch (Exception e) {
                            model.addAttribute("error", true);
                            model.addAttribute("numfound", 0);
                            return "orders";
                        }
                    }
                } catch (NumberFormatException e) {
                    return "redirect:/orderslist";
                }
            } else {
                try {
                    List<OrderAndUserDTO> orders = ordersService.getOrdersByStatus(0, 3).join();
                    model.addAttribute("orders", orders);
                    model.addAttribute("numfound", orders.size());
                    return "orders";
                } catch (Exception e){
                    model.addAttribute("error", true);
                    model.addAttribute("numfound", 0);
                    return "orders";
                }
            }
        }
        return "orders";
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/mycart")
    public String cart(Model model) {
        model.addAttribute("role",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().toString());
        try {
            List<CartGoodDTO> goods = ordersService.getCart().join();
            model.addAttribute("goods", goods);
            return "cart";
        } catch (Exception e){
            model.addAttribute("error", true);
            return "cart";
        }
    }
}
