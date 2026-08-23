package com.soc.orderservice.model;

import java.time.LocalDateTime;

public class DeliveryInfo {
    private String trackingNumber;
    private String carrier;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime estimatedDeliveryTime;
    private String dispatchNotes;

    public DeliveryInfo() {}

    public DeliveryInfo(String trackingNumber, String carrier, DeliveryStatus deliveryStatus, LocalDateTime estimatedDeliveryTime, String dispatchNotes) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.deliveryStatus = deliveryStatus;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.dispatchNotes = dispatchNotes;
    }

    public static DeliveryInfoBuilder builder() {
        return new DeliveryInfoBuilder();
    }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public String getDispatchNotes() { return dispatchNotes; }
    public void setDispatchNotes(String dispatchNotes) { this.dispatchNotes = dispatchNotes; }

    public static class DeliveryInfoBuilder {
        private String trackingNumber;
        private String carrier;
        private DeliveryStatus deliveryStatus;
        private LocalDateTime estimatedDeliveryTime;
        private String dispatchNotes;

        public DeliveryInfoBuilder trackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return this; }
        public DeliveryInfoBuilder carrier(String carrier) { this.carrier = carrier; return this; }
        public DeliveryInfoBuilder deliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; return this; }
        public DeliveryInfoBuilder estimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; return this; }
        public DeliveryInfoBuilder dispatchNotes(String dispatchNotes) { this.dispatchNotes = dispatchNotes; return this; }

        public DeliveryInfo build() {
            return new DeliveryInfo(trackingNumber, carrier, deliveryStatus, estimatedDeliveryTime, dispatchNotes);
        }
    }
}
