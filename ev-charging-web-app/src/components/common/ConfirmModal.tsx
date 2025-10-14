interface ConfirmModalProps {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
  confirmColor?: "green" | "red" | "blue"; // for styling the confirm button
}

const ConfirmModal = ({
  title,
  message,
  confirmText = "Yes",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
  loading = false,
  confirmColor = "green",
}: ConfirmModalProps) => {
  const getConfirmClass = () => {
    switch (confirmColor) {
      case "green":
        return "bg-green-500 text-white hover:bg-green-600";
      case "red":
        return "bg-red-500 text-white hover:bg-red-600";
      case "blue":
        return "bg-blue-500 text-white hover:bg-blue-600";
      default:
        return "bg-green-500 text-white hover:bg-green-600";
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-sm text-center">
        <h2 className="text-lg font-bold mb-2">{title}</h2>
        <p className="mb-4">{message}</p>
        <div className="flex justify-center gap-4">
          <button
            onClick={onConfirm}
            disabled={loading}
            className={`px-4 py-2 rounded font-semibold ${getConfirmClass()}`}
          >
            {loading ? "Processing..." : confirmText}
          </button>
          <button
            onClick={onCancel}
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 font-semibold"
          >
            {cancelText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
