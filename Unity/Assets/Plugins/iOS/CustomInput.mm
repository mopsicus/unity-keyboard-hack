#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

extern UIViewController *UnityGetGLViewController();
extern void	UnitySendMessage(const char *obj, const char *method, const char *msg);

@interface Input : UIViewController <UITextViewDelegate> {
    UITextView *textView;
    bool multiLines;
    NSMutableDictionary *data;
}
@end

@implementation Input;


- (char *)makeStringCopy:(const char *)string {
    return (string == NULL) ? NULL : strcpy((char *)malloc(strlen(string) + 1), string);
}

- (void)showInput:(NSString *)text mode:(bool)mode {
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(onKeyboardHide:) name:UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(onKeyboardWillShow:) name:UIKeyboardDidShowNotification object:nil];
    data = [[NSMutableDictionary alloc] init];
    multiLines = mode;
    textView = [[UITextView alloc] initWithFrame:CGRectMake(2000, 0, 0, 0)];
    [textView setDelegate:self];
    [textView setOpaque: NO];
    [textView setAutocorrectionType:UITextAutocorrectionTypeNo];
    [textView setSpellCheckingType:UITextSpellCheckingTypeNo];
    [textView setTextColor:[UIColor clearColor]];
    [textView setTintColor:[UIColor clearColor]];
    [textView setBackgroundColor:[UIColor clearColor]];
    [UnityGetGLViewController().view addSubview:textView];
    [textView setText:text];
    [textView becomeFirstResponder];
    [textView setNeedsDisplay];
}

-(void)onKeyboardHide:(NSNotification *)notification {
    [self close];
}

-(void)onKeyboardWillShow:(NSNotification *)notification {
    NSDictionary *info  = notification.userInfo;
    NSValue *value = info[UIKeyboardFrameEndUserInfoKey];
    CGRect rawFrame = [value CGRectValue];
    CGRect keyboardFrame = [self.view convertRect:rawFrame fromView:nil];
    CGFloat height = keyboardFrame.size.height;
    [self sendData:1 data:[NSNumber numberWithFloat:height]];
}

- (bool)isLandscape {
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    return (orientation == 0 || orientation == UIInterfaceOrientationPortrait) ? NO : YES;
}

- (void)close {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [textView removeFromSuperview];
    textView = NULL;
    [UnityGetGLViewController().view endEditing:YES];
    [self sendData:0 data:NULL];
    [self sendData:1 data:[NSNumber numberWithInt:0]];
}

- (void)textViewDidChange:(UITextView *)tView {
    if (!multiLines) {
        if ([tView.text length] == 0) {
            [self sendData:2 data:@""];
            return;
        }
        unichar lastChar = [tView.text characterAtIndex:[tView.text length] - 1];
        if (lastChar == '\n')
            [self close];
        else
            [self sendData:2 data:tView.text];
    } else
        [self sendData:2 data:tView.text];
}

- (void)sendData:(int)code data:(NSObject *)info {
    [data removeAllObjects];
    [data setObject:[NSNumber numberWithInt:code] forKey:@"code"];
    if (info != NULL)
        [data setObject:info forKey:@"data"];
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:NSJSONWritingPrettyPrinted error:&error];
    NSString *json = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    UnitySendMessage([self makeStringCopy:[@"Plugins" UTF8String]], [self makeStringCopy:[@"OnCustomInputAction" UTF8String]], [self makeStringCopy:[json UTF8String]]);
}

static Input *input = NULL;

extern "C" {
    
    void inputLaunch (const char* text, bool mode) {
        if (input == NULL)
            input = [[Input alloc] init];
        [input showInput:[NSString stringWithUTF8String:text] mode:mode];
    }
    
    void inputClose () {
        [input close];
    }
    
}


@end
