import React from 'react';

class Popup extends React.PureComponent {
  render() {
    const {
      coordinate, active, viewBox, payload,
    } = this.props;
    if (!active || payload == null || !payload[0]) return null;

    const { status, count } = payload[0].payload;

    const POPUP_HEIGHT = 55;

    const style = {
      left: 0,
      top: coordinate.y - POPUP_HEIGHT - viewBox.top - 4,
      width: 300,
      height: POPUP_HEIGHT,
    };

    return (
      <div>
        <div
          className="crd-popup donut-popup text-center"
          style={style}
        >
          {status}
          :
          {count}
          <div className="arrow" />
        </div>
      </div>
    );
  }
}

export default Popup;
