package com.koosco.orderservice.order.api

import com.koosco.common.core.response.ApiResponse
import com.koosco.commonsecurity.resolver.AuthId
import com.koosco.orderservice.order.application.usecase.CreateOrderUseCase
import com.koosco.orderservice.order.application.usecase.GetOrderDetailUseCase
import com.koosco.orderservice.order.application.usecase.GetOrdersUseCase
import com.koosco.orderservice.order.application.usecase.RefundOrderItemsUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "주문", description = "주문 관리 API")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getOrdersUseCase: GetOrdersUseCase,
    private val getOrderDetailUseCase: GetOrderDetailUseCase,
    private val refundOrderItemsUseCase: RefundOrderItemsUseCase,
) {

    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다. 주문 생성 시 CREATED 상태로 시작됩니다.",
    )
    @PostMapping
    fun createOrder(
        @AuthId userId: Long,
        @Valid @RequestBody request: CreateOrderRequest,
    ): ApiResponse<CreateOrderResponse> {
        val result = createOrderUseCase.execute(request.toCommand(userId))

        return ApiResponse.Companion.success(CreateOrderResponse.Companion.from(result))
    }

    @Operation(
        summary = "주문 목록 조회",
        description = "특정 사용자의 주문 목록을 페이징 처리하여 조회합니다. 기본 페이지 크기는 20입니다.",
    )
    @GetMapping
    fun getOrders(
        @AuthId userId: Long,
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable,
    ): ApiResponse<Page<OrderResponse>> {
        val result = getOrdersUseCase.execute(userId, pageable)
        return ApiResponse.Companion.success(result.map { OrderResponse.Companion.from(it) })
    }

    @Operation(
        summary = "주문 상세 조회",
        description = "주문 상세 정보를 조회합니다. 주문 아이템 정보를 포함합니다.",
    )
    @GetMapping("/{orderId}")
    fun getOrderDetail(@PathVariable orderId: Long): ApiResponse<OrderDetailResponse> {
        val result = getOrderDetailUseCase.execute(orderId)
        return ApiResponse.Companion.success(OrderDetailResponse.Companion.from(result))
    }

    @Operation(
        summary = "주문 아이템 환불",
        description = "주문 아이템을 환불합니다. 부분 환불 및 전체 환불이 가능합니다.",
    )
    @PostMapping("/{orderId}/refund")
    fun refundOrderItems(
        @PathVariable orderId: Long,
        @Valid @RequestBody request: RefundOrderItemsRequest,
    ): ApiResponse<RefundResponse> {
        val result = refundOrderItemsUseCase.execute(request.toCommand(orderId))
        return ApiResponse.Companion.success(RefundResponse.Companion.from(result))
    }
}
