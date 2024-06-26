import React, { useState } from 'react';

import DeceptiveDesignForm from './DeceptiveDesignForm';
import Header from './Header'
import ResultBox from './ResultBox'
import BeatLoader from "react-spinners/BeatLoader";

import logo from './media/veracookie_logo.png';

const App = () => {
  const [isLoading, setLoading] = useState(false);
  const [data, setData] = useState(null);

  const handleFormSubmit = async (url) => {
    setData(null);
    setLoading(true);

    const form = new FormData();
    form.append("link", url.trim().toLowerCase());

    const backend = process.env.REACT_APP_BACKEND_URL;

    try {
      const response = await fetch(backend, {
        "method": "POST",
        "mode": 'cors',
        "body": form
      });

      let data = response.ok ? await response.json() : { "message" : `${await response.text()}` };

      setLoading(false);
      setData(data);
    }
    catch (error) {
      setLoading(false);
      setData( { "message": error.message } );
    }
  };

  return (
    <>
      <Header />
      <div className="container mx-auto p-5 flex flex-col justify-center items-center text-center">
        <img height="72" width="350" src={logo} alt="VeraCookie logo" />
        <p className="w-full my-3 text-base md:text-xl"> A tool that detects deceptive patterns in cookie banners.</p>
        <DeceptiveDesignForm onSubmit={handleFormSubmit} />
        <BeatLoader color={"#ffbd4d"} loading={isLoading} size={14} aria-label="Loading Spinner" data-testid="loader" />
        <ResultBox data={data} />
      </div>
    </>
  );

};

export default App;