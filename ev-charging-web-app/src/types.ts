// ==============================
// User related types
// ==============================
export interface User {
  id: number;
  name: string;
  email: string;
  isActive: boolean;
  role?: string;
  // Add other fields as needed
}

export interface CreateUser {
  name: string;
  email: string;
  password: string;
  role?: string;
}

export interface UpdateUser {
  id: number;
  name?: string;
  email?: string;
  isActive?: boolean;
  role?: string;
}

export interface DeactivateEvOwner {
  id: number;
  reason?: string;
}

export interface ActivateUser {
  id: number;
}

// ==============================
// EV Charging Station types
// ==============================
export interface Station {
  id: number;
  name: string;
  location: string;
  isActive: boolean;
  slots: number;
  // Add other fields as needed
}

export interface CreateStation {
  name: string;
  location: string;
  slots: number;
}

export interface UpdateStation {
  id: number;
  name?: string;
  location?: string;
  slots?: number;
  isActive?: boolean;
}

export interface DeactivateStation {
  id: number;
  reason?: string;
}

// ==============================
// Booking types
// ==============================
export interface Booking {
  id: number;
  stationId: number;
  userId: number;
  slotNumber: number;
  status: "pending" | "approved" | "finalized" | "canceled";
  startTime: string;
  endTime?: string;
}

export interface GetBookingsByStation {
  stationId: number;
}

export interface UpdateSlotAvailability {
  stationId: number;
  slotNumber: number;
  isAvailable: boolean;
}

export interface ApproveBooking {
  bookingId: number;
}

export interface FinalizeBooking {
  bookingId: number;
  endTime: string;
}

// ==============================
// Authentication / Dashboard
// ==============================
export interface AuthenticateUser {
  email: string;
  password: string;
}

export interface DashboardInfo {
  pendingBookingsCount: number;
  approvedBookingsCount: number;
}

// ==============================
// Utility / Response types
// ==============================
// export interface ApiResponse<T = any> {
//   success: boolean;
//   message: string;
//   data?: T;
// }

export interface Notification {
  id: string;
  userId: string;
  message: string;
  createdAt: string;
  isRead: boolean;
}

export interface UserResponse {
  data?: {
    id?: string;
    fullName?: string;
    email?: string;
    role?: string;
    userType?: string;
    isActive?: boolean;
    createdAt?: string;
  };
  status?: number;
}
