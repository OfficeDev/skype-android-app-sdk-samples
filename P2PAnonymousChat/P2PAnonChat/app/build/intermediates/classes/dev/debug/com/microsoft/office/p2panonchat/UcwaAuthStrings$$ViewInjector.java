// Generated code from Butter Knife. Do not modify!
package com.microsoft.office.p2panonchat;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class UcwaAuthStrings$$ViewInjector<T extends com.microsoft.office.p2panonchat.UcwaAuthStrings> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624079, "field 'mUcwaURLEdit'");
    target.mUcwaURLEdit = finder.castView(view, 2131624079, "field 'mUcwaURLEdit'");
    view = finder.findRequiredView(source, 2131624077, "field 'mUcwaTokenEdit'");
    target.mUcwaTokenEdit = finder.castView(view, 2131624077, "field 'mUcwaTokenEdit'");
    view = finder.findRequiredView(source, 2131624080, "field 'mOkButton'");
    target.mOkButton = finder.castView(view, 2131624080, "field 'mOkButton'");
  }

  @Override public void reset(T target) {
    target.mUcwaURLEdit = null;
    target.mUcwaTokenEdit = null;
    target.mOkButton = null;
  }
}
