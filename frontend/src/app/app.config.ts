import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection} from '@angular/core';
import {provideHttpClient} from '@angular/common/http';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeuix/themes/aura'
import {definePreset} from "@primeuix/themes";
import {MessageService} from 'primeng/api';

const BtcTheme = definePreset(Aura, {
  semantic: {
    colorScheme: {
      light: {
        primary: {color: '#00508C', hoverColor: '#00508C'},
        highlight: {
          background: 'rgb(165,165,165)',
          focusBackground: 'rgb(129,129,129)',
          color: '#ffffff',
        },
      },
      dark: {
        primary: {color: '#032270', hoverColor: '#03043c'},
        highlight: {
          background: 'rgb(100,100,100)',
          focusBackground: 'rgb(50,50,50)',
          color: '#ffffff',
        },
      }
    },
  },
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideAnimationsAsync(),
    MessageService,
    providePrimeNG({
      theme: {
        preset: BtcTheme,
        options: {
          fontFamily: '"Roboto", "Open Sans", sans-serif',
          borderRadius: '4px',
          darkModeSelector: 'false',
          cssLayer: {
            name: 'primeng',
            order: 'tailwind, primeng',
          },
        },
      }
    }),
    provideHttpClient()
  ]
};
