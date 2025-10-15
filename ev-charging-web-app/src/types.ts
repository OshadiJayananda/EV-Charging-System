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
// ==============================
// Stations
// ==============================

// Station.ts

export interface Slot {
  slotId: string;
  stationId: string;
  connectorType: string;
  number: number;
  status: string;
}

export type StationType = "AC" | "DC";

export interface Station {
  stationId: string;
  name: string;
  location: string;
  type: StationType;
  capacity: number;
  availableSlots: number; // total slots count
  isActive: boolean;
  slots?: Slot[]; // optional, only when fetching detailed view
}

// Payloads for requests
export interface CreateStationRequest {
  name: string;
  location: string;
  type: StationType;
  capacity: number;
  availableSlots: number;
}

export interface UpdateStationRequest {
  name?: string;
  location?: string;
  type?: StationType;
  capacity?: number;
  availableSlots?: number;
}

export interface PagedResult<T> {
  totalCount: number;
  items: T[];
}

// ==============================
// Booking types
// ==============================
export interface Booking {
  bookingId: string;
  stationId: string;
  slotId: string;
  slotNumber: number;
  timeSlotId: string;
  ownerId: string;
  status: string;
  startTime: string;
  endTime: string;
  createdAt: string;
  updatedAt: string;
  qrCode?: string;
  qrExpiresAt?: string;
  qrImageBase64?: string;
  formattedStartTime?: string;
  formattedEndTime?: string;
  formattedDate?: string;
  ownerName?: string;
  stationName?: string;
  connectorType?: string;
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

export interface PaginatedResponse<T> {
  currentPage: number;
  items: T[];
  pageSize: number;
  totalCount: number;
  totalPages: number;
}

export interface BookingCountResponse {
  todayCount: number;
  futureCount: number;
}
