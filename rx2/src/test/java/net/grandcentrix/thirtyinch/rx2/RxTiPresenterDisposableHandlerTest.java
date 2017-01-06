/*
 * Copyright (C) 2016 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.rx2;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.reactivex.observers.TestObserver;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class RxTiPresenterDisposableHandlerTest {

    private RxTiPresenterDisposableHandler mDisposableHandler;

    private TiPresenter mPresenter;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mPresenter = new TiPresenter() {
        };
        mDisposableHandler = new RxTiPresenterDisposableHandler(mPresenter);
        mView = mock(TiView.class);
    }

    @Test
    public void testManageDisposable_AfterDestroy_ShouldThrowIllegalState() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        final TestObserver<Object> testObserver = new TestObserver<>();

        try {
            mDisposableHandler.manageDisposable(testObserver);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("DESTROYED"));
        }
    }

    @Test
    public void testManageDisposable_WithAlreadyDisposedDisposable_ShouldDoNothing()
            throws Exception {
        final TestObserver<Integer> testObserver = new TestObserver<>();
        testObserver.dispose();
        assertThat(testObserver.isDisposed(), is(true));

        mDisposableHandler.manageDisposable(testObserver);

        assertThat(testObserver.isDisposed(), is(true));
    }

    @Test
    public void testManageDisposable_WithDestroy_ShouldDispose() throws Exception {
        mPresenter.create();
        final TestObserver<Integer> testObserver = new TestObserver<>();

        mDisposableHandler.manageDisposable(testObserver);
        assertThat(testObserver.isDisposed(), is(false));

        mPresenter.destroy();
        assertThat(testObserver.isDisposed(), is(true));
    }

    @Test
    public void testManageVieDisposable_WithDetachSingleDispose_ShouldDispose()
            throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> testObserver = new TestObserver<>();

        mDisposableHandler.manageViewDisposable(testObserver);
        assertThat(testObserver.isDisposed(), is(false));

        mPresenter.detachView();
        assertThat(testObserver.isDisposed(), is(true));
    }

    @Test
    public void testManageViewDisposable_DetachBeforeAttach_ShouldThrowAssertError()
            throws Exception {
        mPresenter.create();
        final TestObserver<Integer> testObserver = new TestObserver<>();

        mDisposableHandler.manageViewDisposable(testObserver);
        mPresenter.detachView();

        // Think about that.
        // Maybe this will never happen. But when! Then we have to dispose all the view disposable.
        // Same in rx module
        assertThat(testObserver.isDisposed(), is(false));
    }

    @Test
    public void testManageViewDisposeable_WithOneAlreadyDisposed_ShouldNotAddToDisposable()
            throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        secondTestObserver.dispose();

        mDisposableHandler.manageViewDisposable(firstTestObserver, secondTestObserver);

        assertThat(firstTestObserver.isDisposed(), is(false));
        assertThat(secondTestObserver.isDisposed(), is(true));
    }


    @Test
    public void testManagerViewDisposable_WithDetach_ShouldDispose() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        final TestObserver<Integer> thirdTestObserver = new TestObserver<>();

        mDisposableHandler
                .manageViewDisposable(firstTestObserver, secondTestObserver, thirdTestObserver);
        assertThat(firstTestObserver.isDisposed(), equalTo(false));
        assertThat(secondTestObserver.isDisposed(), equalTo(false));
        assertThat(thirdTestObserver.isDisposed(), equalTo(false));

        mPresenter.detachView();
        assertThat(firstTestObserver.isDisposed(), equalTo(true));
        assertThat(secondTestObserver.isDisposed(), equalTo(true));
        assertThat(thirdTestObserver.isDisposed(), equalTo(true));
    }
}