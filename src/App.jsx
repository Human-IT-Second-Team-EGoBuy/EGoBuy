import './App.css'
import { BrowserRouter } from "react-router-dom";
import PageRouter from './component/home/PageRouter.jsx';

function App() {

  return (
    <BrowserRouter>
      <PageRouter />
    </BrowserRouter>
  )
}

export default App