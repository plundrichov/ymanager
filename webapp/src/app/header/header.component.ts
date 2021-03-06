import {Component} from '@angular/core';
import {MatDialog} from '@angular/material';
import {LocalizationService} from '../localization/localization.service';
import {UserService} from '../services/api/user.service';
import {UserProfile} from '../models/user.model';
import {ProfileSettingsComponent} from '../profile-settings/profile-settings.component';
import {UsersService} from "../services/api/users.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.sass']
})
export class HeaderComponent {
  profile: UserProfile;
  language: string;

  constructor(
    private dialog: MatDialog,
    private localizationService: LocalizationService,
    private userService: UserService,
    private usersService: UsersService
    ) {
    usersService.getLoggedUserProfile()
      .subscribe((data: UserProfile) => this.profile = data);
    this.language = this.localizationService.getCurrentLanguage();
  }

  switchLanguage(language: string) {
    this.language = this.localizationService.switchLocale(language);
  }
  onProfileClick(): void {
    this.usersService.getLoggedUserProfile()
      .subscribe((data: UserProfile) => {
        this.profile = data;

        this.dialog.open(ProfileSettingsComponent, {
          data: {
            notification: this.profile.notification
          }
        }).afterClosed().subscribe(dialogData => {
          if (!dialogData || !dialogData.isConfirmed) {
            return;
          }

          this.userService.putNotificationSettingsWithLanguage(
            {
              notification: dialogData.notification
            },
            this.localizationService.getCurrentLanguage()
          ).subscribe(() => {
            this.usersService.getLoggedUserProfile().subscribe((profile: UserProfile) => this.profile = profile);
          });
        });
      });
  }
}
