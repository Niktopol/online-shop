package com.example.onlineshop.service;

import com.example.onlineshop.model.dto.*;
import com.example.onlineshop.repository.UserRepository;
import com.example.orders.OrdersServiceGrpc;
import com.example.types.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OrdersService {
    @GrpcClient("orders")
    OrdersServiceGrpc.OrdersServiceStub astub;
    @GrpcClient("orders")
    OrdersServiceGrpc.OrdersServiceBlockingStub bstub;

    UserRepository userRepository;

    public CompletableFuture<List<OrderDTO>> getOrders() {
        CompletableFuture<List<OrderDTO>> future = new CompletableFuture<>();
        List<OrderDTO> orders = new ArrayList<>();

        astub.getUserOrders(Id.newBuilder().setId(
                userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()).build(),
                new StreamObserver<Order>() {
                    @Override
                    public void onNext(Order order) {
                        orders.add(new OrderDTO(order.getId(),
                                order.getGoodsList().stream().map(ordg -> new OrderGoodDTO(
                                        new GoodDTO(ordg.getGood().getId(), ordg.getGood().getName(),
                                                ordg.getGood().getPrice(), ordg.getGood().getAmount(),
                                                ordg.getGood().getCanBeSold()),
                                        ordg.getPrice(),
                                        ordg.getAmount())).toList(),
                                order.getOrderStatus(),
                                order.getPrice()));
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
        List<OrderAndUserDTO> orders = new ArrayList<>();

        astub.getOrdersByStatus(StatusWindow.newBuilder()
                        .setOrderStatusMin(status_min)
                        .setOrderStatusMax(status_max).build(),
                new StreamObserver<OrderAndUser>() {
                    @Override
                    public void onNext(OrderAndUser order) {
                        orders.add(new OrderAndUserDTO(order.getId(),
                                order.getGoodsList().stream().map(ordg -> new OrderGoodDTO(
                                        new GoodDTO(ordg.getGood().getId(), ordg.getGood().getName(),
                                                ordg.getGood().getPrice(), ordg.getGood().getAmount(),
                                                ordg.getGood().getCanBeSold()),
                                        ordg.getPrice(),
                                        ordg.getAmount())).toList(),
                                order.getOrderStatus(),
                                order.getPrice(),
                                order.getUserId()));
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
        List<CartGoodDTO> goods = new ArrayList<>();

        astub.getCartGoods(Id.newBuilder().setId(
                        userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()).build(),
                new StreamObserver<CartGood>() {
                    @Override
                    public void onNext(CartGood cartGood) {
                        goods.add(new CartGoodDTO(
                                new GoodDTO(
                                        cartGood.getGood().getId(),
                                        cartGood.getGood().getName(),
                                        cartGood.getGood().getPrice(),
                                        cartGood.getGood().getAmount(),
                                        cartGood.getGood().getCanBeSold()),
                                cartGood.getAmount()
                        ));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        future.complete(goods);
                    }
                });
        return future;
    }

    public CompletableFuture<OperationResultDTO> addGoodToCart(Long id) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        astub.addCartGood(ManageGoodInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(id).build(),
                responseObserver);
        return future;
    }

    public CompletableFuture<OperationResultDTO> addGoodsToCart(List<Long> ids) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        final StreamObserver<ManageGoodInfo> request = astub.addCartGoods(responseObserver);
        ids.stream()
                .map(id -> ManageGoodInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(id).build())
                .forEach(request::onNext);
        request.onCompleted();

        return future;
    }

    public CompletableFuture<OperationResultDTO> delGoodFromCart(Long id) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        astub.deleteCartGood(ManageGoodInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(id).build(),
                responseObserver);
        return future;
    }

    public CompletableFuture<OperationResultDTO> delGoodsFromCart(List<Long> ids) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        final StreamObserver<ManageGoodInfo> request = astub.deleteCartGoods(responseObserver);
        ids.stream()
                .map(id -> ManageGoodInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(id).build())
                .forEach(request::onNext);
        request.onCompleted();

        return future;
    }

    public CompletableFuture<OperationResultDTO> alterCartGoodAmount(GoodAmountDTO good) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        astub.alterCartGoodAmount(GoodAmountInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(good.getGoodId())
                        .setAmount(good.getAmount()).build(),
                responseObserver);
        return future;
    }

    public CompletableFuture<OperationResultDTO> alterCartGoodsAmount(List<GoodAmountDTO> goods) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        final StreamObserver<GoodAmountInfo> request = astub.alterCartGoodsAmount(responseObserver);
        goods.stream()
                .map(good -> GoodAmountInfo.newBuilder()
                        .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                        .setGoodId(good.getGoodId())
                        .setAmount(good.getAmount()).build())
                .forEach(request::onNext);
        request.onCompleted();

        return future;
    }

    public CompletableFuture<OperationResultDTO> setOrderStatus(Long id, Integer status) {
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
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

        astub.setOrderStatus(OrderStatus.newBuilder()
                        .setId(id)
                        .setStatus(status).build(),
                responseObserver);
        return future;
    }

    public OperationResultDTO createOrder(Boolean buyAvailable) {
        StringResponse response = bstub.createOrder(CreateOrderInfo.newBuilder()
                .setUserId(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId())
                .setBuyMax(buyAvailable).build());
        return new OperationResultDTO(response.getResponseCode(), response.getResponse());
    }
}
