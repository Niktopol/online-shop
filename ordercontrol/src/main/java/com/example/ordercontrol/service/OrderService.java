package com.example.ordercontrol.service;

import com.example.ordercontrol.model.entity.User;
import com.example.ordercontrol.repository.*;
import com.example.orders.OrdersServiceGrpc;
import com.example.types.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@GrpcService
public class OrderService extends OrdersServiceGrpc.OrdersServiceImplBase {
    OrderRepository orderRepository;
    CartRepository cartRepository;
    UserRepository userRepository;
    GoodRepository goodRepository;
    OrderGoodRepository orderGoodRepository;

    @Override
    public void getCartGoods(Id request, StreamObserver<CartGoodList> responseObserver) {
        List<com.example.ordercontrol.model.entity.CartGood> goods = cartRepository.findAllByUser(
                userRepository.findById(request.getId()).get());

        responseObserver.onNext(CartGoodList.newBuilder().addAllGoods(
                goods.stream().map(good -> CartGood.newBuilder()
                        .setGood(Good.newBuilder()
                                .setId(good.getGood().getId())
                                .setName(good.getGood().getName())
                                .setPrice(good.getGood().getPrice())
                                .setAmount(good.getGood().getAmount())
                                .setCanBeSold(good.getGood().getCanBeSold()).build())
                        .setAmount(good.getAmount()).build()).toList()).build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void addCartGoods(ManageGoodInfoList request, StreamObserver<StringResponse> responseObserver) {
        List<com.example.ordercontrol.model.entity.CartGood> cartGoods = new ArrayList<>();
        User user = userRepository.findById(request.getInfos(0).getUserId()).get();
        for (ManageGoodInfo info: request.getInfosList()){
            Optional<com.example.ordercontrol.model.entity.Good> good = goodRepository.findById(info.getGoodId());
            if (good.isPresent()){
                cartGoods.add(new com.example.ordercontrol.model.entity.CartGood(user, good.get()));
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
                return;
            }
        }
        try {
            cartRepository.saveAllAndFlush(cartGoods);
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                    "List contains good that is already in the cart").asRuntimeException());
            return;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Goods added to the cart successfully").build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void deleteCartGoods(ManageGoodInfoList request, StreamObserver<StringResponse> responseObserver) {
        List<com.example.ordercontrol.model.entity.CartGood> cartGoods = new ArrayList<>();
        User user = userRepository.findById(request.getInfos(0).getUserId()).get();
        for (ManageGoodInfo info: request.getInfosList()){
            Optional<com.example.ordercontrol.model.entity.Good> good = goodRepository.findById(info.getGoodId());
            if (good.isPresent()){
                Optional<com.example.ordercontrol.model.entity.CartGood> cartGood = cartRepository.findByUserAndGood(user, good.get());
                if (cartGood.isPresent()){
                    cartGoods.add(cartGood.get());
                } else {
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                            "List contains good that is not in the cart").asRuntimeException());
                    return;
                }
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
                return;
            }
        }
        try {
            cartRepository.deleteAll(cartGoods);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Goods deleted from the cart successfully").build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void alterCartGoodsAmount(GoodAmountInfoList request, StreamObserver<StringResponse> responseObserver) {
        List<com.example.ordercontrol.model.entity.CartGood> cartGoods = new ArrayList<>();
        User user = userRepository.findById(request.getInfos(0).getUserId()).get();
        for (GoodAmountInfo info: request.getInfosList()){
            Optional<com.example.ordercontrol.model.entity.Good> good = goodRepository.findById(info.getGoodId());
            if (good.isPresent()){
                Optional<com.example.ordercontrol.model.entity.CartGood> cartGood = cartRepository.findByUserAndGood(user, good.get());
                if (cartGood.isPresent()){
                    cartGood.get().setAmount(info.getAmount());
                    cartGoods.add(cartGood.get());
                } else {
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                            "List contains good that is not in the cart").asRuntimeException());
                    return;
                }
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
                return;
            }
        }
        try {
            cartRepository.saveAllAndFlush(cartGoods);
        } catch (OptimisticLockException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription(
                    "Listed good has already been modified").asRuntimeException());
            return;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Goods amount updated successfully").build());
        responseObserver.onCompleted();
    }

    private OrderGood constructOrderGood(com.example.ordercontrol.model.entity.OrderGood good){
        return OrderGood.newBuilder()
                .setGood(Good.newBuilder()
                        .setId(good.getGoodType().getId())
                        .setName(good.getGoodType().getName())
                        .setPrice(good.getGoodType().getPrice())
                        .setAmount(good.getGoodType().getAmount())
                        .setCanBeSold(good.getGoodType().getCanBeSold()).build())
                .setAmount(good.getAmount())
                .setPrice(good.getPrice()).build();
    }

    @Override
    public void getUserOrders(Id request, StreamObserver<OrderList> responseObserver) {
        List<com.example.ordercontrol.model.entity.Order> orders = orderRepository.findAllByUser(
                userRepository.findById(request.getId()).get());
        responseObserver.onNext(OrderList.newBuilder()
                .addAllOrders(orders.stream().map(ord -> Order.newBuilder()
                        .setId(ord.getId())
                        .setOrderStatus(ord.getStatus())
                        .setPrice(ord.getPrice())
                        .addAllGoods(ord.getGoods().stream().map(this::constructOrderGood).toList()).build()).toList()).build());

        responseObserver.onCompleted();
    }

    @Override
    public void getOrdersByStatus(StatusWindow request, StreamObserver<OrderAndUserList> responseObserver) {
        List<com.example.ordercontrol.model.entity.Order> orders = orderRepository.findByStatusBetween(
                request.getOrderStatusMin(),
                request.getOrderStatusMax());
        responseObserver.onNext(OrderAndUserList.newBuilder()
                .addAllOrders(orders.stream().map(ord -> OrderAndUser.newBuilder()
                        .setId(ord.getId())
                        .setOrderStatus(ord.getStatus())
                        .setPrice(ord.getPrice())
                        .setUserId(ord.getUser().getId())
                        .addAllGoods(ord.getGoods().stream().map(this::constructOrderGood).toList()).build()).toList()).build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void setOrderStatus(OrderStatus request, StreamObserver<StringResponse> responseObserver) {
        Optional<com.example.ordercontrol.model.entity.Order> orderOpt = orderRepository.findById(request.getId());
        if (orderOpt.isPresent()){
            try{
                com.example.ordercontrol.model.entity.Order order = orderOpt.get();
                order.setStatus(request.getStatus());
                orderRepository.saveAndFlush(order);
            } catch (OptimisticLockException e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription(
                        "Order status has already been modified").asRuntimeException());
                return;
            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                return;
            }
            responseObserver.onNext(StringResponse.newBuilder()
                    .setResponse("Order status changed successfully").build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown order id").asRuntimeException());
        }
    }

    @Transactional
    @Override
    public void createOrder(CreateOrderInfo request, StreamObserver<StringResponse> responseObserver) {
        User user = userRepository.findById(request.getUserId()).get();
        List<com.example.ordercontrol.model.entity.CartGood> cartGoods = cartRepository.findAllByUser(user);
        List<com.example.ordercontrol.model.entity.OrderGood> orderGoods = new ArrayList<>();
        List<com.example.ordercontrol.model.entity.Good> goodsBought = new ArrayList<>();
        com.example.ordercontrol.model.entity.Order order = new com.example.ordercontrol.model.entity.Order(0.0, user);
        Iterator<com.example.ordercontrol.model.entity.CartGood> iterator = cartGoods.iterator();
        while (iterator.hasNext()) {
            com.example.ordercontrol.model.entity.CartGood good = iterator.next();
            int amount;
            if (request.getBuyMax()) {
                amount = Math.min(good.getAmount(), good.getGood().getAmount());
            } else {
                if (good.getGood().getAmount() < good.getAmount()){
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not enough goods available").asRuntimeException());
                    return;
                }
                amount = good.getAmount();
            }
            if (good.getGood().getCanBeSold()){
                if (amount > 0){
                    good.getGood().setAmount(good.getGood().getAmount() - amount);
                    goodsBought.add(good.getGood());
                    orderGoods.add(new com.example.ordercontrol.model.entity.OrderGood(
                            good.getGood(),
                            good.getGood().getPrice() * amount,
                            amount,
                            order));
                    order.setPrice(order.getPrice() + good.getGood().getPrice() * amount);
                } else {
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not enough goods available").asRuntimeException());
                    return;
                }
            } else {
                iterator.remove();
            }
        }
        if (!goodsBought.isEmpty()){
            try {
                goodRepository.saveAll(goodsBought);
                goodRepository.flush();
                cartRepository.saveAll(cartGoods);
                cartRepository.flush();
                cartRepository.deleteAll(cartGoods);
                orderRepository.save(order);
                orderGoodRepository.saveAll(orderGoods);
                orderGoodRepository.flush();
            } catch (OptimisticLockException e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription(
                        "Goods details has been updated").asRuntimeException());
                return;
            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                return;
            }
            responseObserver.onNext(StringResponse.newBuilder()
                    .setResponse("Order created").build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Can't create empty order").asRuntimeException());
        }
    }
}
