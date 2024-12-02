package com.example.onlineshop.service;

import com.example.onlineshop.model.dto.*;
import com.example.onlineshop.repository.UserRepository;
import com.example.orders.OrdersServiceGrpc;
import com.example.types.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OrdersService {
    @GrpcClient("orders")
    OrdersServiceGrpc.OrdersServiceStub astub;
    @GrpcClient("orders")
    OrdersServiceGrpc.OrdersServiceBlockingStub bstub;
    @Autowired
    UserRepository userRepository;

    public CompletableFuture<List<OrderDTO>> getOrders() {
        CompletableFuture<List<OrderDTO>> future = new CompletableFuture<>();

        astub.getUserOrders(Id.newBuilder().setId(
                userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()).build(),
                new StreamObserver<OrderList>() {
                    List<OrderDTO> orders;
                    @Override
                    public void onNext(OrderList orderList) {
                        orders = orderList.getOrdersList().stream().map(ord -> new OrderDTO(
                                ord.getId(),
                                ord.getGoodsList().stream().map(ordG -> new OrderGoodDTO(
                                        new GoodDTO(ordG.getGood().getId(),
                                                ordG.getGood().getName(),
                                                ordG.getGood().getPrice(),
                                                ordG.getGood().getAmount(),
                                                ordG.getGood().getCanBeSold()),
                                        ordG.getPrice(),
                                        ordG.getAmount()
                                )).toList(),
                                ord.getOrderStatus(),
                                ord.getPrice()
                        )).toList();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        future.complete(orders);
                    }
                });

        return future;
    }

    public CompletableFuture<List<OrderAndUserDTO>> getOrdersByStatus(Integer status_min, Integer status_max) {
        CompletableFuture<List<OrderAndUserDTO>> future = new CompletableFuture<>();

        astub.getOrdersByStatus(StatusWindow.newBuilder().setOrderStatusMin(status_min).setOrderStatusMax(status_max).build(),
                new StreamObserver<OrderAndUserList>() {
                    List<OrderAndUserDTO> orders;
                    @Override
                    public void onNext(OrderAndUserList orderList) {
                        orders = orderList.getOrdersList().stream().map(ord -> new OrderAndUserDTO(
                                ord.getId(),
                                ord.getGoodsList().stream().map(ordG -> new OrderGoodDTO(
                                        new GoodDTO(ordG.getGood().getId(),
                                                ordG.getGood().getName(),
                                                ordG.getGood().getPrice(),
                                                ordG.getGood().getAmount(),
                                                ordG.getGood().getCanBeSold()),
                                        ordG.getPrice(),
                                        ordG.getAmount()
                                )).toList(),
                                ord.getOrderStatus(),
                                ord.getPrice(),
                                ord.getUserId()
                        )).toList();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        future.complete(orders);
                    }
                });

        return future;
    }

    public CompletableFuture<List<CartGoodDTO>> getCart() {
        CompletableFuture<List<CartGoodDTO>> future = new CompletableFuture<>();

        astub.getCartGoods(Id.newBuilder().setId(
                        userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()).build(),
                new StreamObserver<CartGoodList>() {
                    List<CartGoodDTO> cart;
                    @Override
                    public void onNext(CartGoodList goods) {
                        cart = goods.getGoodsList().stream().map(
                                good -> new CartGoodDTO(
                                        new GoodDTO(
                                                good.getGood().getId(),
                                                good.getGood().getName(),
                                                good.getGood().getPrice(),
                                                good.getGood().getAmount(),
                                                good.getGood().getCanBeSold()),
                                        good.getAmount())).toList();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        future.complete(cart);
                    }
                });
        return future;
    }

    public static StreamObserver<StringResponse> getStringResponseStreamObserver(CompletableFuture<String> future) {
        final String[] result = new String[1];

        return new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = stringResponse.getResponse();
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(result[0]);
            }
        };
    }


    public CompletableFuture<String> addGoodsToCart(List<Long> ids) {
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);
        Long userId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId();

        astub.addCartGoods(ManageGoodInfoList.newBuilder().addAllInfos(ids.stream().map(id -> ManageGoodInfo.newBuilder()
                .setUserId(userId)
                .setGoodId(id).build()).toList()).build(), responseObserver);

        return future;
    }

    public CompletableFuture<String> delGoodsFromCart(List<Long> ids) {
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);
        Long userId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId();

        astub.deleteCartGoods(ManageGoodInfoList.newBuilder().addAllInfos(ids.stream().map(id -> ManageGoodInfo.newBuilder()
                .setUserId(userId)
                .setGoodId(id).build()).toList()).build(), responseObserver);

        return future;
    }

    public CompletableFuture<String> alterCartGoodsAmount(List<GoodAmountDTO> goods) {
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);
        Long userId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId();

        astub.alterCartGoodsAmount(GoodAmountInfoList.newBuilder().addAllInfos(goods.stream().map(good -> GoodAmountInfo.newBuilder()
                .setUserId(userId)
                .setGoodId(good.getGoodId())
                .setAmount(good.getAmount()).build()).toList()).build(), responseObserver);

        return future;
    }

    public CompletableFuture<String> setOrderStatus(Long id, Integer status) {
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);

        astub.setOrderStatus(OrderStatus.newBuilder()
                        .setId(id)
                        .setStatus(status).build(),
                responseObserver);
        return future;
    }

    public String createOrder(Boolean buyAvailable) {
        StringResponse response = bstub.createOrder(CreateOrderInfo.newBuilder()
                .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                .setBuyMax(buyAvailable).build());
        return response.getResponse();
    }
}
