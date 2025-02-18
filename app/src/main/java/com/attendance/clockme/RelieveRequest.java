package com.attendance.clockme;

    public class RelieveRequest {
        private String requestId;
        private String username;
        private String position;
        private String email;
        private String date;
        private String reason;
        private String status;

        public RelieveRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(RelieveRequest.class)
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestId() {
            return requestId; // Ensure this getter is included
        }

        public String getUsername() {
            return username;
        }

        public String getPosition() {
            return position;
        }

        public String getEmail() {
            return email;
        }

        public String getDate() {
            return date;
        }

        public String getReason() {
            return reason;
        }

        public String getStatus() {
            return status;
        }
}
