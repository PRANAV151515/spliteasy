import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// har request ke saath token apne aap lag jaata hai
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;

