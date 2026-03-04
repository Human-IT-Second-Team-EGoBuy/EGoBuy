import { useState } from "react";
import { getUserById, activateUser, deleteUser, blackUser } from "../../../../../api/admin/adminUsers";
import UserDetailModal from "./UserDetailModal";
import "./AdminUsersPage.css";

export default function AdminUsersPage() {
  const [userId, setUserId] = useState("");
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);

  const onSearch = async () => {
    setError(null);
    setLoading(true);
    try {
      const data = await getUserById(Number(userId));
      setUser(data);
    } catch (e) {
      setUser(null);
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const onAction = async (type) => {
    if (!user?.userId && !user?.id) return;
    const id = user.userId ?? user.id;

    setLoading(true);
    setError(null);
    try {
      if (type === "active") await activateUser(id);
      if (type === "delete") await deleteUser(id);
      if (type === "black") await blackUser(id);
      const refreshed = await getUserById(id);
      setUser(refreshed);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-users">
      <div className="admin-users__header">
        <h1 className="admin-users__title">유저 관리</h1>
        <p className="admin-users__desc">userId로 유저 조회 후 상태 변경(Active/Black/Delete)</p>
      </div>

      <div className="admin-users__search">
        <input
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="userId 입력"
          className="admin-users__input"
        />
        <button
          onClick={onSearch}
          disabled={loading || !userId}
          className="admin-users__btn admin-users__btn--primary"
        >
          조회
        </button>
      </div>

      {error && <div className="admin-users__error">{error}</div>}

      {user && (
        <div className="admin-users__result">
          <div className="admin-users__actions">
            <button
              onClick={() => onAction("active")}
              className="admin-users__btn admin-users__btn--active"
            >
              Active로 변경
            </button>
            <button
              onClick={() => onAction("black")}
              className="admin-users__btn admin-users__btn--black"
            >
              블랙리스트
            </button>
            <button
              onClick={() => onAction("delete")}
              className="admin-users__btn admin-users__btn--delete"
            >
              SoftDelete(탈퇴)
            </button>
          </div>

          <UserDetailModal user={user} onClose={() => setUser(null)} />
        </div>
      )}
    </div>
  );
}