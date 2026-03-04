import "./UserDetailModal.css";

export default function UserDetailModal({ user, onClose }) {
  if (!user) return null;

  return (
    <div className="user-modal">
      <div className="user-modal__card">
        <div className="user-modal__header">
          <h2 className="user-modal__title">유저 상세</h2>
          <button onClick={onClose} className="user-modal__close">
            닫기
          </button>
        </div>

        <pre className="user-modal__content">
          {JSON.stringify(user, null, 2)}
        </pre>
      </div>
    </div>
  );
}