import { useEffect, useState } from "react";
import { postRequest, putRequest, getRequest } from "../../components/common/api";
import type { CreateStationRequest, Station } from "../../types";
import { useNavigate, useParams } from "react-router-dom";

const StationForm = ({ isEdit = false }: { isEdit?: boolean }) => {
    console.log(isEdit);
    const [form, setForm] = useState<CreateStationRequest>({
        name: "",
        location: "",
        type: "AC",
        capacity: 0,
        availableSlots: 0,
    });
    const navigate = useNavigate();
    const { stationId } = useParams();

    // 🔹 Fetch existing station details when editing
    useEffect(() => {
        const fetchStation = async () => {
            if (isEdit && stationId) {
                const res = await getRequest<Station>(`/station/${stationId}`);
                if (res) {
                    setForm({
                        name: res.data.name,
                        location: res.data.location,
                        type: res.data.type,
                        capacity: res.data.capacity,
                        availableSlots: res.data.availableSlots,
                    });
                }
            }
        };
        fetchStation();
    }, [isEdit, stationId]);

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async () => {
        if (isEdit && stationId) {
            await putRequest(`/station/${stationId}`, form);
        } else {
            await postRequest("/station", form);
        }
        navigate("/admin/stations");
    };

    return (
        <div className="max-w-xl mx-auto p-6 bg-white shadow-lg rounded-lg mt-20">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {isEdit ? "Edit Station" : "Add New Station"}
            </h2>

            <div className="space-y-5">
                {/* Station Name */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Station Name
                    </label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        placeholder="Enter station name"
                        className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:outline-none"
                    />
                </div>

                {/* Location */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Location
                    </label>
                    <input
                        name="location"
                        value={form.location}
                        onChange={handleChange}
                        placeholder="Enter location"
                        className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:outline-none"
                    />
                </div>

                {/* Type */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Type
                    </label>
                    <select
                        name="type"
                        value={form.type}
                        onChange={handleChange}
                        className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:outline-none"
                    >
                        <option value="AC">AC</option>
                        <option value="DC">DC</option>
                    </select>
                </div>

                {/* Capacity */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Capacity
                    </label>
                    <input
                        type="number"
                        name="capacity"
                        value={form.capacity}
                        onChange={handleChange}
                        placeholder="Enter capacity"
                        className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:outline-none"
                    />
                </div>

                {/* Available Slots */}
                {isEdit && (
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Available Slots
                        </label>
                        <input
                            type="number"
                            name="availableSlots"
                            value={(form as any).availableSlots ?? ""}
                            onChange={handleChange}
                            placeholder="Enter available slots"
                            className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:outline-none"
                        />
                    </div>
                )}

                {/* Submit Button */}
                <button
                    onClick={handleSubmit}
                    className="w-full py-3 bg-green-600 text-white font-semibold rounded-md shadow hover:bg-green-700 transition"
                >
                    {isEdit ? "Update Station" : "Create Station"}
                </button>
            </div>
        </div>
    );
};

export default StationForm;
