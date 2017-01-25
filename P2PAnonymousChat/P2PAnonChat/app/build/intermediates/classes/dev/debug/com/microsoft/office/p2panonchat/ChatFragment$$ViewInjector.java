// Generated code from Butter Knife. Do not modify!
package com.microsoft.office.p2panonchat;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class ChatFragment$$ViewInjector<T extends com.microsoft.office.p2panonchat.ChatFragment> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624066, "field 'mMessageSendEditText'");
    target.mMessageSendEditText = finder.castView(view, 2131624066, "field 'mMessageSendEditText'");
    view = finder.findRequiredView(source, 2131624067, "field 'mSendButton'");
    target.mSendButton = finder.castView(view, 2131624067, "field 'mSendButton'");
  }

  @Override public void reset(T target) {
    target.mMessageSendEditText = null;
    target.mSendButton = null;
  }
}
