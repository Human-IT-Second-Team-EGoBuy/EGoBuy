import { Outlet } from "react-router-dom";
import AdminSideNav from "./AdminSideNav";
import "./AdminLayout.css";

export default function AdminLayout() {
  return (
    <div className="admin-layout">
      <AdminSideNav />
      <main className="admin-layout__main">
        <Outlet />
      </main>
    </div>
  );
}