package com.soc.orderservice.dto;

import com.soc.orderservice.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UpdateDeliveryRequest {

    @NotNull(message = "Delivery status is required")
    private DeliveryStatus deliveryStatus;

    private String carrier;
    private String trackingNumber;
    private LocalDateTime estimatedDeliveryTime;
    private String dispatchNotes;

    public UpdateDeliveryRequest() {}

    public UpdateDeliveryRequest(DeliveryStatus deliveryStatus, String carrier, String trackingNumber, LocalDateTime estimatedDeliveryTime, String dispatchNotes) {
        this.deliveryStatus = deliveryStatus;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.dispatchNotes = dispatchNotes;
    }

    public static UpdateDeliveryRequestBuilder builder() {
        return new UpdateDeliveryRequestBuilder();
    }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public String getDispatchNotes() { return dispatchNotes; }
    public void setDispatchNotes(String dispatchNotes) { this.dispatchNotes = dispatchNotes; }

    public static class UpdateDeliveryRequestBuilder {
        private DeliveryStatus deliveryStatus;
        private String carrier;
        private String trackingNumber;
        private LocalDateTime estimatedDeliveryTime;
        private String dispatchNotes;

        public UpdateDeliveryRequestBuilder deliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; return this; }
        public UpdateDeliveryRequestBuilder carrier(String carrier) { this.carrier = carrier; return this; }
        public UpdateDeliveryRequestBuilder trackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return this; }
        public UpdateDeliveryRequestBuilder estimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; return this; }
        public UpdateDeliveryRequestBuilder dispatchNotes(String dispatchNotes) { this.dispatchNotes = dispatchNotes; return this; }

        public UpdateDeliveryRequest build() {
            return new UpdateDeliveryRequest(deliveryStatus, carrier, trackingNumber, estimatedDeliveryTime, dispatchNotes);
        }
    }
}
