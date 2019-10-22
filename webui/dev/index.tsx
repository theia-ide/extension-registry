import * as ReactDOM from 'react-dom';
import * as React from 'react';
import { Main } from '../src/main';
import { BrowserRouter } from 'react-router-dom';

const node = document.getElementById('main');
ReactDOM.render(<BrowserRouter><Main /></BrowserRouter>, node);
