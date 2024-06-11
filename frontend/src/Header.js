import logo from './media/veracookie_logo.png';

const Header = () => {
    return (
        <div className="navbar p-5">
            <img height="48" width="175" src={logo} alt="VeraCookie logo" />
        </div>
    )
};

export default Header;