import {Component, EventEmitter, Inject, OnInit, Output} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {UserType} from '../../enums/common.enum';
import {UserProfile} from '../../models/user.model';
import {UserSettings} from '../../models/settings.model';
import {TranslateService} from '@ngx-translate/core';


@Component({
  selector: 'app-edit-employee-dialog',
  templateUrl: './edit-employee-dialog.component.html',
  styleUrls: ['./edit-employee-dialog.component.sass']
})
export class EditEmployeeDialogComponent implements OnInit {
  readonly _userTypes: string[] = ['EMPLOYER', 'EMPLOYEE'];
  private _sickDaysCount: number;
  private _addVacationHoursCount: number;
  private _userType: UserType;
  private readonly _userId: number;

  @Output() postUserSettings = new EventEmitter<UserSettings>();

  constructor(public dialogRef: MatDialogRef<EditEmployeeDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: UserProfile,
              private snackBar: MatSnackBar,
              private translateService: TranslateService
  ) {
    this._sickDaysCount = data.sickDayCount;
    this._addVacationHoursCount = 0;
    this._userType = data.role;
    this._userId = data.id;
  }

  ngOnInit() {
  }

  onConfirmClick(): void {
    if (this._sickDaysCount == null || this._addVacationHoursCount == null || this._userType == null) {
      this.translateService.get('error.missingField').subscribe((res: string) => {
        this.snackBar.open(res, 'X', { duration: 5000 });
      });
    } else {
      this.postUserSettings.emit({
        id: this._userId,
        role: this._userType,
        sickDayCount: this._sickDaysCount,
        vacationCount: this._addVacationHoursCount
      });

      this.dialogRef.close();
    }
  }

  onCloseClick(): void {
    this.dialogRef.close();
  }

}
