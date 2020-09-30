import React from 'react';
import { Link } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';

export const Layout: React.FC<{}> = ({ children }) => (
  <>
    <nav className='navbar navbar-expand-lg navbar-dark bg-dark'>
      <div className='collapse navbar-collapse'>
        <ul className='navbar-nav'>
          <li className='nav-item'>
            <Link className='nav-link' to='/package/list'>Packages</Link>
          </li>
        </ul>
      </div>
    </nav>
    <div className='container-fluid'>
      {children}
    </div>
    <ToastContainer />
  </>
);
