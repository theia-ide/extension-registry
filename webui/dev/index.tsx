import * as ReactDOM from 'react-dom';
import * as React from 'react';
import { Main } from '../src/main';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@material-ui/styles';
import { createMuiTheme } from '@material-ui/core';

const theme = createMuiTheme({
    palette: {
        primary: { main: '#EEEEEE', contrastText: '#263238' },
        secondary: { main: '#42A5F5' }
    }
});

const node = document.getElementById('main');
ReactDOM.render(<BrowserRouter>
<ThemeProvider theme={theme}>
<Main
apiUrl={window.location.protocol + "//" + window.location.hostname + "/api"}
listHeaderTitle='Extensions for VS Code Compatible Editors'
logoURL='./open-source.png'
pageTitle='Open VSX Registry'
/>
</ThemeProvider>
</BrowserRouter>, node);
