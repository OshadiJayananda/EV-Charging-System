import axios, { AxiosError } from "axios";
import toast from "react-hot-toast";

const API_BASE_URL =
  import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:3000/api";

// Create Axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 10000,
});

// Optional: Auth token interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers ??= {} as typeof config.headers;
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Optional: Global logging interceptor
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    console.error("API Error:", error.response?.status, error.response?.data);
    return Promise.reject(error);
  }
);

// Helper to extract error message and show toast
const handleError = (error: unknown, fallbackMessage: string) => {
  const axiosError = error as AxiosError;
  const data = axiosError.response?.data;
  const message =
    (data && typeof data === "object" && "message" in data
      ? (data as { message?: string }).message
      : undefined) ||
    axiosError.message ||
    fallbackMessage;

  toast.error(message);
  return null;
};

// GET request
export const getRequest = async <T>(
  url: string,
  params?: object
): Promise<T | null> => {
  try {
    const response = await api.get<T>(url, { params });
    return response.data;
  } catch (error) {
    return handleError(error, "Error fetching data");
  }
};

// POST request
export const postRequest = async <T>(
  url: string,
  data?: object
): Promise<T | null> => {
  try {
    const response = await api.post<T>(url, data);
    return response.data;
  } catch (error) {
    return handleError(error, "Error posting data");
  }
};

// PUT request
export const putRequest = async <T>(
  url: string,
  data?: object
): Promise<T | null> => {
  try {
    const response = await api.put<T>(url, data);
    return response.data;
  } catch (error) {
    return handleError(error, "Error updating data");
  }
};

// DELETE request
export const deleteRequest = async <T>(url: string): Promise<T | null> => {
  try {
    const response = await api.delete<T>(url);
    return response.data;
  } catch (error) {
    return handleError(error, "Error deleting data");
  }
};

export default api;
