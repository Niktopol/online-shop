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
    public void getCartGoods(Id request, StreamObserver<CartGood> responseObserver) {
        List<com.example.ordercontrol.model.entity.CartGood> goods = cartRepository.findAllByUser(
                userRepository.findById(request.getId()).get());
        for (com.example.ordercontrol.model.entity.CartGood good: goods){
            responseObserver.onNext(CartGood.newBuilder()
                    .setGood(Good.newBuilder()
                            .setId(good.getGood().getId())
                            .setName(good.getGood().getName())
                            .setPrice(good.getGood().getPrice())
                            .setAmount(good.getGood().getAmount())
                            .setCanBeSold(good.getGood().getCanBeSold())
                            .build())
                    .setAmount(good.getAmount()).build());
        }
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void addCartGood(ManageGoodInfo request, StreamObserver<StringResponse> responseObserver) {
        Optional<com.example.ordercontrol.model.entity.Good> good = goodRepository.findById(request.getGoodId());
        if (good.isPresent()) {
            com.example.ordercontrol.model.entity.CartGood cartGood = new com.example.ordercontrol.model.entity.CartGood(
                    userRepository.findById(request.getUserId()).get(),
                    good.get()
            );
            try {
                cartRepository.saveAndFlush(cartGood);
            } catch (DataIntegrityViolationException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                        String.format("Good with id %d is already in the cart", request.getGoodId())).asRuntimeException());
                return;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                return;
            }
            responseObserver.onNext(StringResponse.newBuilder()
                    .setResponse("Good added to the cart successfully").build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
        }
    }

    @Transactional
    private void saveCartGoods(List<com.example.ordercontrol.model.entity.CartGood> goods){
        cartRepository.saveAll(goods);
        cartRepository.flush();
    }

    @Override
    public StreamObserver<ManageGoodInfo> addCartGoods(StreamObserver<StringResponse> responseObserver) {
        return new StreamObserver<ManageGoodInfo>() {
            final List<ManageGoodInfo> goodsInfo = new ArrayList<>();

            @Override
            public void onNext(ManageGoodInfo manageGoodInfo) {
                goodsInfo.add(manageGoodInfo);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                List<com.example.ordercontrol.model.entity.CartGood> goods = new ArrayList<>();
                for (ManageGoodInfo info: goodsInfo){
                    if (goodRepository.existsById(info.getGoodId())){
                        goods.add(new com.example.ordercontrol.model.entity.CartGood(
                                userRepository.findById(info.getUserId()).get(),
                                goodRepository.findById(info.getGoodId()).get()
                        ));
                    } else {
                        responseObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("List contains unknown good id").asRuntimeException());
                        return;
                    }
                }
                try {
                    saveCartGoods(goods);
                } catch (DataIntegrityViolationException e){
                    responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                            "List contains good that is already in the cart").asRuntimeException());
                    return;
                } catch (Exception e){
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Goods added to the cart successfully").build());
                responseObserver.onCompleted();
            }
        };
    }

    @Transactional
    @Override
    public void deleteCartGood(ManageGoodInfo request, StreamObserver<StringResponse> responseObserver) {
        if (goodRepository.existsById(request.getGoodId())){
            Optional<com.example.ordercontrol.model.entity.CartGood> good = cartRepository.findByUserAndGood(
                    userRepository.findById(request.getUserId()).get(),
                    goodRepository.findById(request.getUserId()).get());
            if (good.isPresent()){
                try {
                    cartRepository.delete(good.get());
                } catch (Exception e){
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Good deleted from the cart successfully").build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                        String.format("Good with id '%d' is not in the cart", request.getGoodId())).asRuntimeException());
            }
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
        }
    }

    @Transactional
    private void deleteCartGoods(List<com.example.ordercontrol.model.entity.CartGood> goods){
        cartRepository.deleteAll(goods);
    }

    @Override
    public StreamObserver<ManageGoodInfo> deleteCartGoods(StreamObserver<StringResponse> responseObserver) {
        return new StreamObserver<ManageGoodInfo>() {
            final List<ManageGoodInfo> goodsInfo = new ArrayList<>();

            @Override
            public void onNext(ManageGoodInfo manageGoodInfo) {
                goodsInfo.add(manageGoodInfo);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                List<com.example.ordercontrol.model.entity.CartGood> goods = new ArrayList<>();
                for (ManageGoodInfo info: goodsInfo){
                    if (goodRepository.existsById(info.getGoodId())){
                        goods.add(new com.example.ordercontrol.model.entity.CartGood(
                                userRepository.findById(info.getUserId()).get(),
                                goodRepository.findById(info.getGoodId()).get()
                        ));
                    } else {
                        responseObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("List contains unknown good id").asRuntimeException());
                        return;
                    }
                }
                try {
                    deleteCartGoods(goods);
                } catch (Exception e){
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Goods added to the cart successfully").build());
                responseObserver.onCompleted();
            }
        };
    }

    @Transactional
    @Override
    public void alterCartGoodAmount(GoodAmountInfo request, StreamObserver<StringResponse> responseObserver) {
        Optional<com.example.ordercontrol.model.entity.Good> goodOpt = goodRepository.findById(request.getGoodId());
        if (goodOpt.isPresent()){
            Optional<com.example.ordercontrol.model.entity.CartGood> cartGoodOpt = cartRepository.findByUserAndGood(
                    userRepository.findById(request.getUserId()).get(),
                    goodOpt.get()
            );
            if (cartGoodOpt.isPresent()){
                try{
                    com.example.ordercontrol.model.entity.CartGood good = cartGoodOpt.get();
                    good.setAmount(request.getAmount());
                    cartRepository.saveAndFlush(good);
                } catch (OptimisticLockException e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    responseObserver.onError(Status.INTERNAL.withDescription(
                            "Listed good has been already modified").asRuntimeException());
                    return;
                } catch (Exception e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
            }else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                        String.format("Good with id '%d' is not in the cart", request.getGoodId())).asRuntimeException());
                return;
            }
            responseObserver.onNext(StringResponse.newBuilder()
                    .setResponse("Good modified successfully").build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Unknown good id").asRuntimeException());
        }
    }

    @Transactional
    private void alterCartGoodsAmount(List<com.example.ordercontrol.model.entity.CartGood> goods){
        cartRepository.saveAll(goods);
        cartRepository.flush();
    }

    @Override
    public StreamObserver<GoodAmountInfo> alterCartGoodsAmount(StreamObserver<StringResponse> responseObserver) {
        return new StreamObserver<GoodAmountInfo>() {
            final List<GoodAmountInfo> goodsInfo = new ArrayList<>();

            @Override
            public void onNext(GoodAmountInfo goodAmountInfo) {
                goodsInfo.add(goodAmountInfo);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                List<com.example.ordercontrol.model.entity.CartGood> goods = new ArrayList<>();
                for (GoodAmountInfo info: goodsInfo){
                    if (goodRepository.existsById(info.getGoodId())){
                        Optional<com.example.ordercontrol.model.entity.CartGood> cartGoodOpt = cartRepository.findByUserAndGood(
                                userRepository.findById(info.getUserId()).get(),
                                goodRepository.findById(info.getGoodId()).get()
                        );
                        if (cartGoodOpt.isPresent()){
                            com.example.ordercontrol.model.entity.CartGood good = cartGoodOpt.get();
                            good.setAmount(info.getAmount());
                            goods.add(good);
                        } else {
                            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                                    "List contains good that is not in the cart").asRuntimeException());
                            return;
                        }
                    } else {
                        responseObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("List contains unknown good id").asRuntimeException());
                        return;
                    }
                }
                try {
                    alterCartGoodsAmount(goods);
                } catch (OptimisticLockException e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    responseObserver.onError(Status.INTERNAL.withDescription(
                            "Listed good has been already modified").asRuntimeException());
                    return;
                } catch (Exception e){
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Goods modified successfully").build());
                responseObserver.onCompleted();
            }
        };
    }

    private OrderGood constructOrderGood(com.example.ordercontrol.model.entity.OrderGood good){
        return OrderGood.newBuilder()
                .setGood(Good.newBuilder()
                        .setId(good.getGoodType().getId())
                        .setName(good.getGoodType().getName())
                        .setPrice(good.getGoodType().getPrice())
                        .setAmount(good.getGoodType().getAmount())
                        .setCanBeSold(good.getGoodType().getCanBeSold()).build())
                .setPrice(good.getPrice())
                .setPrice(good.getPrice()).build();
    }

    @Override
    public void getUserOrders(Id request, StreamObserver<Order> responseObserver) {
        List<com.example.ordercontrol.model.entity.Order> orders = orderRepository.findAllByUser(
                userRepository.findById(request.getId()).get());
        for (com.example.ordercontrol.model.entity.Order order: orders){
            responseObserver.onNext(Order.newBuilder()
                    .setId(order.getId())
                    .setOrderStatus(order.getStatus())
                    .setPrice(order.getPrice())
                    .addAllGoods(order.getGoods().stream().map(this::constructOrderGood).toList()).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getOrdersByStatus(StatusWindow request, StreamObserver<OrderAndUser> responseObserver) {
        List<com.example.ordercontrol.model.entity.Order> orders = orderRepository.findByStatusBetween(
                request.getOrderStatusMin(),
                request.getOrderStatusMax());
        for (com.example.ordercontrol.model.entity.Order order: orders){
            responseObserver.onNext(OrderAndUser.newBuilder()
                    .setId(order.getId())
                    .setOrderStatus(order.getStatus())
                    .setPrice(order.getPrice())
                    .setUserId(order.getUser().getId())
                    .addAllGoods(order.getGoods().stream().map(this::constructOrderGood).toList()).build());
        }
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
        for (com.example.ordercontrol.model.entity.CartGood good: cartGoods) {
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
            if (amount > 0 && good.getGood().getCanBeSold()){
                good.getGood().setAmount(good.getGood().getAmount() - amount);
                goodsBought.add(good.getGood());
                orderGoods.add(new com.example.ordercontrol.model.entity.OrderGood(
                        good.getGood(),
                        good.getGood().getPrice() * amount,
                        amount,
                        order));
                order.setPrice(order.getPrice() + good.getGood().getPrice() * amount);
            } else {
                cartGoods.remove(good);
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
