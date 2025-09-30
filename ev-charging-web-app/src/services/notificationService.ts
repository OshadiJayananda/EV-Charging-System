import { HubConnectionBuilder, LogLevel } from "@microsoft/signalr";

export function createNotificationConnection(
  onReceive: (notification: any) => void
) {
  const connection = new HubConnectionBuilder()
    .withUrl("/notificationHub", {
      accessTokenFactory: () => localStorage.getItem("token") || "",
    })
    .configureLogging(LogLevel.Information)
    .withAutomaticReconnect()
    .build();

  connection.on("ReceiveNotification", onReceive);

  connection.start().catch(console.error);

  return connection;
}
