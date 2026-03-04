import { NavLink } from "react-router-dom";
import "./AdminSideNav.css";

const Item = ({ to, label }) => (
  <NavLink
    to={to}
    className={({ isActive }) =>
      `admin-sidenav__item ${
        isActive
          ? "admin-sidenav__item--active"
          : "admin-sidenav__item--inactive"
      }`
    }
  >
    {label}
  </NavLink>
);

export default function AdminSideNav() {
  return (
    <aside className="admin-sidenav">
      <div className="admin-sidenav__title">관리자</div>
      <div className="admin-sidenav__menu">
        <Item to="/admin/users" label="유저 관리" />
        <Item to="/admin/reports" label="신고 관리" />
        <Item to="/admin/qna" label="문답 관리" />
      </div>
    </aside>
  );
}