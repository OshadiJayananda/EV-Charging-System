import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:3000/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

// Example of a GET request
export const getRequest = async <T>(
  url: string,
  params?: object
): Promise<T> => {
  const response = await api.get<T>(url, { params });
  return response.data;
};

// Example of a POST request
export const postRequest = async <T>(
  url: string,
  data?: object
): Promise<T> => {
  const response = await api.post<T>(url, data);
  return response.data;
};

// You can add more methods (put, delete, etc.) as needed

export default api;
