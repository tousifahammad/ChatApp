package com.bibhas.handsapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bibhas.handsapp.Fragments.ChatsFragment;
import com.bibhas.handsapp.Fragments.FriendsFragment;
import com.bibhas.handsapp.Fragments.GroupsFragment;

/**
 * Created by Bibhas on 21-Aug-18.
 */


class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {


            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return  chatsFragment;

            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            case 2:
                GroupsFragment groupsFragment=new GroupsFragment();
                return groupsFragment;

            default:
                return  null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch (position) {

            case 0:
                return "CHATS";

            case 1:
                return "FRIENDS";

            case 2:
                return "GROUPS";

            default:
                return null;
        }

    }

}
