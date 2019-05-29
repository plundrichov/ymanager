import {Injectable} from '@angular/core';
import {UserService} from '../api/user.service';
import {UserProfile} from '../../models/user.model';
import {MenuItem} from '../../models/menu-item.model';
import {UserType} from '../../enums/common.enum';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  constructor(private userService: UserService) {
  }

  getMenuItems() {
    const menuItems: MenuItem[] = [];

    return new Observable((observer) => {
      this.userService.getLoggedUserProfile()
        .subscribe((profile: UserProfile) => {
            menuItems.push({name: 'Dashboard', routePath: 'dashboard'});
            if (profile.role === UserType.EMPLOYER) {
              menuItems.push({name: 'Zaměstnanci', routePath: 'employees'});
            }

            observer.next(menuItems);
            observer.complete();
          },
          () => {
            observer.next(menuItems);
            observer.complete();
          });
    });
  }
}
