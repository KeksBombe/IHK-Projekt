import {Component, OnInit, ViewChild} from '@angular/core';
import {MenubarModule} from "primeng/menubar";
import {ToastModule} from "primeng/toast";
import {MenuModule} from "primeng/menu";
import {Button} from "primeng/button";
import {MenuItem} from "primeng/api";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    MenubarModule,
    ToastModule,
    MenuModule
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  items: MenuItem[] | undefined;

  ngOnInit() {
    const html = document.querySelector('html');

    this.items = [
      {
        label: 'Optionen',
        items: [
          {
            label: 'Zur BTC',
            icon: 'pi pi-upload',
            command: () => this.openBtcHomepage(),
          },
          {
            label: 'Seite aktualisieren',
            icon: 'pi pi-refresh',
            command: () => this.refreshPage(),
          },

        ]
      }
    ];
  }

  refreshPage() {
    window.location.reload();
  }

  openBtcHomepage() {
    window.open('https://www.btc-ag.com/');
  }

}
