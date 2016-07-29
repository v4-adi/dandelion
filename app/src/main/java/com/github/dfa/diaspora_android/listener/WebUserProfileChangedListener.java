/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.dfa.diaspora_android.listener;

/**
 * Created by gsantner (https://gsantner.github.io/) on 26.03.16.
 */
public interface WebUserProfileChangedListener {
    void onUserProfileNameChanged(String name);

    void onUserProfileAvatarChanged(String avatarUrl);

    void onNotificationCountChanged(int notificationCount);

    void onUnreadMessageCountChanged(int unreadMessageCount);
}
