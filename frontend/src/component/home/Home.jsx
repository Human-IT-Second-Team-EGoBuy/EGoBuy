import Header from "./header/Header";
import { Outlet } from "react-router-dom";


export default function Home() {
    return (
        <div className="home-layout">
            <div className="header-layout">
                <Header />
            </div>

            <div className="page-layout">
                <Outlet />
            </div>
        </div>
    )
}