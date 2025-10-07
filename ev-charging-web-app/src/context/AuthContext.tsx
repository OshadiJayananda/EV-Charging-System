// src/context/AuthContext.tsx
import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import {
  getUserRoleFromToken,
  getUserIdFromToken,
} from "../components/common/RoleBasedAccess";

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  userRole: string | null;
  userId: string | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("token")
  );
  const [userRole, setUserRole] = useState<string | null>(
    token ? getUserRoleFromToken(token) : null
  );
  const [userId, setUserId] = useState<string | null>(
    token ? getUserIdFromToken(token) : null
  );

  useEffect(() => {
    if (token) {
      localStorage.setItem("token", token);
      setUserRole(getUserRoleFromToken(token));
      setUserId(getUserIdFromToken(token));
    } else {
      localStorage.removeItem("token");
      setUserRole(null);
      setUserId(null);
    }
  }, [token]);

  const login = (newToken: string) => {
    setToken(newToken);
  };

  const logout = () => {
    setToken(null);
  };

  const contextValue = React.useMemo(
    () => ({
      token,
      isAuthenticated: !!token,
      userRole,
      userId,
      login,
      logout,
    }),
    [token, userRole, userId, login, logout]
  );

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
