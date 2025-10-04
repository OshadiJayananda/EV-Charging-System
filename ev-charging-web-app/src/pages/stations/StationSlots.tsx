import { useEffect, useState } from "react";
import { getRequest, postRequest, deleteRequest } from "../../components/common/api";
import type { Slot } from "../../types";
import { useParams } from "react-router-dom";

const StationSlots = () => {
  const [slots, setSlots] = useState<Slot[]>([]);
  const { stationId } = useParams();

  const fetchSlots = async () => {
    const res = await getRequest<Slot[]>(`/station/${stationId}/slots`);
    if (res) setSlots(res.data);
  };

  const handleAddSlot = async () => {
    await postRequest(`/station/${stationId}/slots`, { number: slots.length + 1 });
    fetchSlots();
  };

  const handleDeleteSlot = async (slotId: string) => {
    await deleteRequest(`/station/${stationId}/slots/${slotId}`);
    fetchSlots();
  };

  useEffect(() => {
    fetchSlots();
  }, [stationId]);

  return (
    <div>
      <h2>Manage Slots</h2>
      <button onClick={handleAddSlot}>+ Add Slot</button>
      <ul>
        {slots.map((slot) => (
          <li key={slot.slotId}>
            Slot #{slot.slotId} – {slot.status ? "Available" : "Occupied"}
            <button onClick={() => handleDeleteSlot(slot.slotId)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default StationSlots;
