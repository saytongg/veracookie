import React, { useState } from 'react';

const DeceptiveDesignForm = ({ onSubmit }) => {
  const [url, setUrl] = useState('');
  const [disableSubmit, setDisableSubmit] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setDisableSubmit(true);
    await onSubmit(url);
    setDisableSubmit(false);
  };

  const handleChange = (e) => {
    e.target.setCustomValidity("");
    setUrl(e.target.value);
  }

  const handleInvalid = (e) => {
    e.target.setCustomValidity("Please submit a valid link.");
  }

  return (
    <form className="w-full md:w-[750px] my-4" onSubmit={handleSubmit}>
      <div className="h-[60px] flex justify-center p-2 border-2 border-black rounded-md bg-white">
        <input className="w-full p-2 border-0 focus:outline-none" type="text" placeholder="Example.com" value={url} onChange={handleChange} onInvalid={handleInvalid} required />
        <input className="p-2 rounded-md bg-[#ffbd4d] text-white cursor-pointer hover:bg-green-500 disabled:cursor-not-allowed disabled:bg-gray-500" type="submit" value="Submit" disabled={disableSubmit} />
      </div>
    </form>
  );

};

export default DeceptiveDesignForm;