const textRatingSettings = {
    "text": "The cookie banner text is",

    "GOOD": {
        "color": "text-green-600",
        "description": "The wording in the cookie notification clearly defines where the collected data will be used."
    },

    "BAD": {
        "color": "text-red-600",
        "description": "The wording in the cookie notification vaguely defines where the collected data will be used."
    }
};

const optionsRatingSettings = {
    "text": "The options are",

    "EVEN": {
        "color": "text-green-600",
        "description": "The cookie banner shows both options to accept and refuse (or manage) cookies, and they are equally easy to see. "
    },

    "WEIGHTED": {
        "color": "text-orange-600",
        "description": "The choice to say no to or manage cookies is made harder to find than the choice to say yes. Because of this, many users end up saying yes to cookies without meaning to, since it's easier to do."
    },

    "ABSENT": {
        "color": "text-red-600",
        "description": "The interface doesn't have a way to say no to or manage cookies, so users can't control if they want to use them or not. This means users don't get a basic choice about their privacy settings."
    },
};

const RatingsBox = ({ setting, rating }) => {
    return (
        <div className="w-full md:w-[750px] mb-5 p-3 flex flex-col justify-center border rounded-[10px] border-[#D3D3D3] bg-white">
            <div className="w-full m-1 p-1 text-[20px]">{setting["text"]} <span className={`font-semibold ${setting[rating]["color"]}`}>{rating}</span></div>
            <div className="w-full m-1 p-1 text-[15px]">{setting[rating]["description"]}</div>
        </div>
    )
};

const ResultBox = ({ data }) => {
    return (data === null ? null :
        data["message"] !== undefined || data["image"] === null?
            <div className="w-full md:w-[750px] p-5 text-red-600">{data["image"] === null ? "No cookie banner found" : data["message"]}</div> :
            <div>
                <h3 className="my-5 text-left font-bold">Here's what we got...</h3>
                <div className="p-7 border border-[#D3D3D3] rounded-[10px]">
                    <img className="mb-5 w-full h-full" alt="Cookie banner image" src={`data:image/png;base64,${data["image"]}`} />
                    <RatingsBox setting={textRatingSettings} rating={data["textRating"]} />
                    <RatingsBox setting={optionsRatingSettings} rating={data["imageRating"]} />
                </div>
            </div>
    );
}

export default ResultBox;