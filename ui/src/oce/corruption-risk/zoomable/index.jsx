import React from 'react';
import { cloneChild } from '../tools';
import './style.scss';

class Zoomable extends React.PureComponent {
  constructor(...args) {
    super(...args);
    this.state = this.state || {};
    this.state.zoomed = false;
  }

  maybeGetZoomed() {
    const { children, ...props } = this.props;
    const { zoomed } = this.state;
    const width = window.innerWidth * 0.9;
    const padding = 150;
    if (zoomed) {
      const style = {
        width,
        marginLeft: -(width / 2),
        paddingTop: 50,
        paddingBottom: 50,
        paddingLeft: padding,
        paddingRight: padding,
      };
      return (
        <div>
          <div className="crd-fullscreen-popup-overlay" onClick={() => this.setState({ zoomed: false })} />
          <div className="crd-fullscreen-popup" style={style}>
            {cloneChild(this, {
              ...props,
              zoomed: true,
              width: width - padding * 2,
            })}
          </div>
        </div>
      );
    }
    return null;
  }

  interceptClicks({ target }) {
    if (typeof target.className === 'string' && target.className.indexOf('zoom-button') > -1) {
      this.setState({ zoomed: true });
    }
  }

  render() {
    const { zoomed } = this.state;
    const { children, ...props } = this.props;
    return (
      <div className="zoomable" onClick={this.interceptClicks.bind(this)}>
        {this.maybeGetZoomed()}
        {!zoomed && cloneChild(this, {
          ...props,
        })}
      </div>
    );
  }
}

export default Zoomable;
