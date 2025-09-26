import type { NavigateFunction } from "react-router-dom";

function getUserRoleFromToken(token: string): string | null {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return null;

    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );

    const payload = JSON.parse(jsonPayload) as {
      role?: string;
      UserType?: string;
    };

    if (payload.role) {
      return payload.role.toLowerCase();
    }
    return null;
  } catch (err) {
    console.error("Failed to parse token", err);
    return null;
  }
}

function roleNavigate(role: string | null, navigate: NavigateFunction) {
  if (role === "admin") {
    navigate("/admin/dashboard");
  } else if (role === "operator") {
    navigate("/operator/dashboard");
  } else {
    navigate("/login");
  }
}

export { getUserRoleFromToken, roleNavigate };
